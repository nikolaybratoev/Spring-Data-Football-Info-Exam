package softuni.exam.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.domain.dtos.PictureSeedRootDto;
import softuni.exam.domain.entities.Picture;
import softuni.exam.repository.PictureRepository;
import softuni.exam.util.ValidatorUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.GlobalConstants.PICTURES_FILE_PATH;

@Service
public class PictureServiceImpl implements PictureService {

    private final ModelMapper modelMapper;
    private final ValidatorUtil validatorUtil;
    private final PictureRepository pictureRepository;
    private final XmlParser xmlParser;

    public PictureServiceImpl(ModelMapper modelMapper,
                              ValidatorUtil validatorUtil,
                              PictureRepository pictureRepository,
                              XmlParser xmlParser) {
        this.modelMapper = modelMapper;
        this.validatorUtil = validatorUtil;
        this.pictureRepository = pictureRepository;
        this.xmlParser = xmlParser;
    }

    @Override
    public String importPictures() throws JAXBException, FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();

        PictureSeedRootDto pictureSeedRootDto = this.xmlParser
                .unmarshalFromFile(PICTURES_FILE_PATH, PictureSeedRootDto.class);

        pictureSeedRootDto.getPictures()
                .forEach(pictureSeedDto -> {
                    if (this.getPictureByUrl(pictureSeedDto.getUrl()) != null) {
                        stringBuilder.append("Already in DB.");
                        return;
                    }

                    if (this.validatorUtil.isValid(pictureSeedDto)) {
                        Picture picture = this.modelMapper
                                .map(pictureSeedDto, Picture.class);

                        this.pictureRepository.saveAndFlush(picture);

                        stringBuilder.append(String
                                .format("Successfully imported picture - %s",
                                        picture.getUrl()));
                    } else {
                        stringBuilder.append("Invalid picture");
                    }

                    stringBuilder.append(System.lineSeparator());
                });

        return stringBuilder.toString().trim();
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesXmlFile() throws IOException {
        return Files.readString(Path.of(PICTURES_FILE_PATH));
    }

    @Override
    public Picture getPictureByUrl(String url) {
        return this.pictureRepository.findFirstByUrl(url);
    }
}
