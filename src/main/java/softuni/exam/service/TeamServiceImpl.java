package softuni.exam.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.domain.dtos.TeamSeedRootDto;
import softuni.exam.domain.entities.Picture;
import softuni.exam.domain.entities.Team;
import softuni.exam.repository.TeamRepository;
import softuni.exam.util.ValidatorUtil;
import softuni.exam.util.XmlParser;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.GlobalConstants.TEAMS_FILE_PATH;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final XmlParser xmlParser;
    private final ModelMapper modelMapper;
    private final TeamRepository teamRepository;
    private final PictureService pictureService;
    private final ValidatorUtil validatorUtil;

    public TeamServiceImpl(XmlParser xmlParser,
                           ModelMapper modelMapper,
                           TeamRepository teamRepository,
                           PictureService pictureService,
                           ValidatorUtil validatorUtil) {
        this.xmlParser = xmlParser;
        this.modelMapper = modelMapper;
        this.teamRepository = teamRepository;
        this.pictureService = pictureService;
        this.validatorUtil = validatorUtil;
    }

    @Override
    public String importTeams() throws JAXBException, FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        TeamSeedRootDto teamSeedRootDto = this.xmlParser
                .unmarshalFromFile(TEAMS_FILE_PATH, TeamSeedRootDto.class);

        teamSeedRootDto.getTeamSeedDtos()
                .forEach(teamSeedDto -> {
                    if (this.getTeamByName(teamSeedDto.getName()) != null) {
                        stringBuilder.append("Already in DB.");
                        return;
                    }

                    if (this.validatorUtil.isValid(teamSeedDto)) {
                        if (this.pictureService.getPictureByUrl(teamSeedDto.getPicture().getUrl()) != null) {
                            Team team = this.modelMapper
                                    .map(teamSeedDto, Team.class);

                            Picture picture = this.pictureService
                                    .getPictureByUrl(teamSeedDto.getPicture().getUrl());

                            team.setPicture(picture);

                            this.teamRepository.saveAndFlush(team);

                            stringBuilder.append(String.format("Successfully imported - %s",
                                    teamSeedDto.getName()));
                        } else {
                            stringBuilder.append("Picture doesn't exists.");
                        }
                    } else {
                        stringBuilder.append("Invalid team.");
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public boolean areImported() {
        return this.teamRepository.count() > 0;
    }

    @Override
    public String readTeamsXmlFile() throws IOException {
        return Files.readString(Path.of(TEAMS_FILE_PATH));
    }

    @Override
    public Team getTeamByName(String name) {
        return this.teamRepository.findFirstByName(name);
    }
}
